package otomatik_vites_proje;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.defuzzifier.Defuzzifier;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierCenterOfArea;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierCenterOfGravity;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierLeftMostMax;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierMeanMax;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierRightMostMax;
import net.sourceforge.jFuzzyLogic.rule.Rule;
import net.sourceforge.jFuzzyLogic.rule.Variable;

public class OtomatikVites {

	// FCL dosyasindaki isimler (birebir ayni olmali)
	public static final String FCL_DOSYASI = "model.fcl";
	public static final String FONKSIYON_BLOGU = "model";
	public static final String KURAL_BLOGU = "blok";
	public static final String CIKIS = "vites_degisimi";

	// Girdilerin sinir degerleri (FCL'deki araliklarla ayni)
	public static final double YUK_MIN = 0, YUK_MAX = 500;        // kg
	public static final double DEVIR_MIN = 600, DEVIR_MAX = 7000;   // devir/dakika
	public static final double EGIM_MIN = -15, EGIM_MAX = 15;     // derece

	// Cikis degerini karara cevirme esikleri
	private static final double KUCULT_ESIGI = -0.33;
	private static final double BUYUT_ESIGI = 0.33;

	private final FIS fis;
	private final double aracYuku;
	private final double motorDevri;
	private final double yolEgimi;

	public OtomatikVites(double aracYuku, double motorDevri, double yolEgimi) throws URISyntaxException {
		this.aracYuku = aracYuku;
		this.motorDevri = motorDevri;
		this.yolEgimi = yolEgimi;
		fis = modeliYukle();
		hesapla(fis);
	}

	// Derste gosterilen yontem: FCL dosyasi bu sinifin paketinde aranir
	private FIS modeliYukle() throws URISyntaxException {
		File dosya = new File(getClass().getResource(FCL_DOSYASI).toURI());
		FIS model = FIS.load(dosya.getPath(), true);
		if (model == null) {
			throw new IllegalStateException(FCL_DOSYASI + " yuklenemedi, dosya adini ve icerigini kontrol edin.");
		}
		return model;
	}

	private void hesapla(FIS model) {
		model.setVariable("arac_yuku", aracYuku);
		model.setVariable("motor_devri", motorDevri);
		model.setVariable("yol_egimi", yolEgimi);
		model.evaluate();
	}

	public FIS getModel() {
		return fis;
	}

	public Variable getCikisDegiskeni() {
		return fis.getVariable(CIKIS);
	}

	public double getSonuc() {
		return getCikisDegiskeni().getValue();
	}

	public String getKarar() {
		double sonuc = getSonuc();
		if (sonuc <= KUCULT_ESIGI) return "VITES KUCULT (azalt)";
		if (sonuc >= BUYUT_ESIGI) return "VITES BUYUT (arttir)";
		return "VITESI KORU (sabit)";
	}

	// Sonuca katki veren (destek derecesi > 0 olan) kurallar
	public List<Rule> getCalisanKurallar() {
		List<Rule> calisanlar = new ArrayList<>();
		for (Rule kural : fis.getFunctionBlock(FONKSIYON_BLOGU).getFuzzyRuleBlock(KURAL_BLOGU).getRules()) {
			if (kural.getDegreeOfSupport() > 0) calisanlar.add(kural);
		}
		return calisanlar;
	}

	// Ayni girdileri farkli bir durulama yontemiyle hesaplar.
	// yontem: "MM" (MOM), "COG", "COA", "LM", "RM"
	public double durulamaIleHesapla(String yontem) throws URISyntaxException {
		FIS yeniModel = modeliYukle();
		Variable cikis = yeniModel.getVariable(CIKIS);
		cikis.setDefuzzifier(durulayiciOlustur(yontem, cikis));
		hesapla(yeniModel);
		return cikis.getValue();
	}

	private static Defuzzifier durulayiciOlustur(String yontem, Variable cikis) {
		switch (yontem) {
			case "MM":  return new DefuzzifierMeanMax(cikis);
			case "COG": return new DefuzzifierCenterOfGravity(cikis);
			case "COA": return new DefuzzifierCenterOfArea(cikis);
			case "LM":  return new DefuzzifierLeftMostMax(cikis);
			case "RM":  return new DefuzzifierRightMostMax(cikis);
			default: throw new IllegalArgumentException("Bilinmeyen durulama yontemi: " + yontem);
		}
	}

	@Override
	public String toString() {
		return "Arac Yuku: " + aracYuku + " kg"
			+ "\nMotor Devri: " + motorDevri + " d/dk"
			+ "\nYol Egimi: " + yolEgimi + " derece"
			+ "\nVites Degisimi : " + String.format("%.3f", getSonuc())
			+ "\nKarar: " + getKarar();
	}
}