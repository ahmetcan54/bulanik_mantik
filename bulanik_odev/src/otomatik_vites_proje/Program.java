package otomatik_vites_proje;

import java.net.URISyntaxException;
import java.util.Scanner;

import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;
import net.sourceforge.jFuzzyLogic.rule.Rule;

public class Program {

	public static void main(String[] args) throws URISyntaxException {
		Scanner in = new Scanner(System.in);

		double aracYuku = degerOku(in, "Arac Yuku (kg)",
				OtomatikVites.YUK_MIN, OtomatikVites.YUK_MAX);
		double motorDevri = degerOku(in, "Motor Devri (devir/dakika)",
				OtomatikVites.DEVIR_MIN, OtomatikVites.DEVIR_MAX);
		double yolEgimi = degerOku(in, "Yol Egimi (derece, inis icin negatif)",
				OtomatikVites.EGIM_MIN, OtomatikVites.EGIM_MAX);

		OtomatikVites vites = new OtomatikVites(aracYuku, motorDevri, yolEgimi);

		// 1) Sonuc
		System.out.println("\n===== SONUC =====");
		System.out.println(vites);

		// 2) Calisan kurallar (destek derecesi > 0)
		System.out.println("\n===== CALISAN KURALLAR =====");
		for (Rule kural : vites.getCalisanKurallar()) {
			System.out.println(kural);
		}

		// 3) Durulama yontemlerinin karsilastirmasi
		System.out.println("\n===== DURULAMA YONTEMLERI =====");
		String[] yontemler = { "MM", "COG", "COA", "LM", "RM" };
		String[] adlar = { "MOM (Mean of Maximum)", "COG (Center of Gravity)",
				"COA (Center of Area)", "LM (Left Most Maximum)", "RM (Right Most Maximum)" };
		for (int i = 0; i < yontemler.length; i++) {
			double sonuc = vites.durulamaIleHesapla(yontemler[i]);
			System.out.printf("%-26s: %7.3f%n", adlar[i], sonuc);
		}

		// 4) Grafikler
		// Uyelik fonksiyonlari (tum girdiler ve cikis)
		JFuzzyChart.get().chart(vites.getModel().getFunctionBlock(OtomatikVites.FONKSIYON_BLOGU));
		// Sadece tarali alan (durulamaya giren bolge)
		JFuzzyChart.get().chart(vites.getCikisDegiskeni().getDefuzzifier(), "Vites Degisimi", true);
	}

	// Kullanicidan aralik disi veya hatali giris kabul etmeden deger okur.
	// Hem 2.5 hem 2,5 yazimini kabul eder.
	private static double degerOku(Scanner in, String mesaj, double min, double max) {
		while (true) {
			System.out.print(mesaj + " [" + sayi(min) + " - " + sayi(max) + "]: ");
			String giris = in.nextLine().trim().replace(',', '.');
			try {
				double deger = Double.parseDouble(giris);
				if (deger >= min && deger <= max) return deger;
				System.out.println("  Deger aralik disinda, tekrar girin.");
			} catch (NumberFormatException e) {
				System.out.println("  Gecerli bir sayi girin.");
			}
		}
	}

	private static String sayi(double d) {
		return d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
	}
}