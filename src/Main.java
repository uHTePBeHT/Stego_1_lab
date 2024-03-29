import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        String imagePath = "src/image.png"; //Ссылка на изображение-шаблон
        String message = "!Vladislav Lavrenko!"; //Сообщение
        String outputImagePath = "src/outputImage.png"; //Ссылка на изображение с сообщением

        showStartMenu(imagePath, message, outputImagePath);
//        hideMessage(imagePath, message, outputImagePath);
//
//        String answer = extractMessage(outputImagePath);
//        System.out.println("Extracted message: " + answer);
//
//        // Норма Минковского
//        double minkowskiNorm = calculateMinkowskiNorm(imagePath, outputImagePath);
//        System.out.println("Minkowski norm = " + minkowskiNorm);
//
//        // Среднее квадратичное отклонение MSE
//        double mse = calculateMSE(imagePath, outputImagePath);
//        System.out.println("MSE = " + mse);
//
//        // Максимальное абсолютное отклонение maxD
//        int maxDeviation = calculateMaxDeviation(imagePath, outputImagePath);
//        System.out.println("Max Deviation = " + maxDeviation);
    }

    private static void hideMessage(String imagePath, String message, String outputImagePath){

        try {
            BufferedImage image = ImageIO.read(new File(imagePath)); //Считываем изображение
            int imageWidth = image.getWidth(); //Получаем длину и ширину
            int imageHeight = image.getHeight();
            int maxMessageLength = (imageWidth * imageHeight) / 8; // получаем максимально возможную длину сообщения
            if (message.length() + 8 > maxMessageLength) {  //Проверка на то, подойдет ли изображение для сокрытия или нет)
                System.out.println("Сообщение слишком длинное для данного изображения");
                return;
            }

            String messageLengthBinary = String.format("%32s", Integer.toBinaryString(message.length())).replace(' ', '0');
            int index = 0;

            for (int i = 0; i < 16; i++) { // В первые 16 пикселей меняем два младших бита
                int pixel = image.getRGB(index % imageWidth, index / imageWidth); // Получаем пиксель

                // Сбросим два младших бита
                pixel &= 0xFFFFFFFC;

                // Установим два младших бита в зависимости от сообщения
                pixel |= ((messageLengthBinary.charAt(2 * i) - '0') << 1) | (messageLengthBinary.charAt(2 * i + 1) - '0');

                // Устанавливаем пиксель в результат
                image.setRGB(index % imageWidth, index / imageWidth, pixel);

                index++; // Двигаемся дальше
            }


            // Скрытие сообщения в изображении
            byte[] messageBytes = message.getBytes(); //Преобразуем сообщение в последовательность байтов
            System.out.println(Arrays.toString(messageBytes)); //Вывод в консоль для проверки

            for (byte b : messageBytes) { // Проходим по байтовому представлению сообщения
                for (int j = 6; j >= 0; j -= 2) {
                    int pixel = image.getRGB(index % imageWidth, index / imageWidth);
                    int oldPixel = pixel;
                    pixel &= 0xFFFFFFFC; // Сбросим два младших бита
                    pixel |= ((b >> (6 - j)) & 3); // Установим два младших бита в зависимости от байта сообщения
                    image.setRGB(index % imageWidth, index / imageWidth, pixel);
                    index++;
                }
            }

            ImageIO.write(image, "png", new File(outputImagePath));
            System.out.println("Сообщение успешно скрыто в изображении");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extractMessage(String outputImagePath) {
        try {
            BufferedImage outputImage = ImageIO.read(new File(outputImagePath)); //Считываем изображение
            int imageWidth = outputImage.getWidth(); // Получаем длину изображения

            // Извлечение длины сообщения из изображения
            StringBuilder lengthBinary = new StringBuilder();
            int index = 0;

            for (int i = 0; i < 16; i++) { // Цикл для извлечения из двух младших бит в 16 пикселях
                int pixel = outputImage.getRGB(index % imageWidth, index / imageWidth);
                // Извлекаем два младших бита и добавляем их к строке
                for (int j = 1; j >= 0; j--) {
                    lengthBinary.append((pixel >> j) & 1);
                }
                index++;
            }

            int messageLength = Integer.parseInt(lengthBinary.toString(), 2); // Преобразуем строку в целочисленный тип

            // Извлечение сообщения из изображения (из двух младших бит)
            StringBuilder messageBinary = new StringBuilder();


            for (int i = 0; i < messageLength; i++) {
                for (int j = 3; j >= 0; j--) {
                    int pixel = outputImage.getRGB((index + j) % imageWidth, (index + j) / imageWidth);
                    messageBinary.append((pixel >> 1) & 1); // Извлекаем первый бит
                    messageBinary.append(pixel & 1);         // Извлекаем второй бит
                }
                index += 4;
            }


// Преобразование бинарного сообщения в строку
            StringBuilder extractedMessage = new StringBuilder();

            for (int i = 0; i < messageBinary.length(); i += 8) {
                extractedMessage.append((char) Integer.parseInt(messageBinary.substring(i, i + 8), 2));
            }

            byte[] messageBytes = extractedMessage.toString().getBytes();
            System.out.println(Arrays.toString(messageBytes));
            return extractedMessage.toString(); // Возвращаем сообщение

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static double calculateMinkowskiNorm(String imagePath, String outputImagePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        BufferedImage outputImage = ImageIO.read(new File(outputImagePath));
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;
        int p = 2;

        double sum = 0;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int originalPixel = image.getRGB(i, j);
                int extractedPixel = outputImage.getRGB(i, j);

                int originalRed = (originalPixel >> 16) & 0xFF;
                int originalGreen = (originalPixel >> 8) & 0xFF;
                int originalBlue = originalPixel & 0xFF;

                int extractedRed = (extractedPixel >> 16) & 0xFF;
                int extractedGreen = (extractedPixel >> 8) & 0xFF;
                int extractedBlue = extractedPixel & 0xFF;

                double pixelDiffRed = Math.pow(Math.abs(originalRed - extractedRed), p);
                double pixelDiffGreen = Math.pow(Math.abs(originalGreen - extractedGreen), p);
                double pixelDiffBlue = Math.pow(Math.abs(originalBlue - extractedBlue), p);

                sum += pixelDiffRed + pixelDiffGreen + pixelDiffBlue;
            }
        }

        double norm = Math.pow(sum / totalPixels, 1.0 / p);
        return norm;
    }

    private static double calculateMSE(String imagePath, String outputImagePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        BufferedImage outputImage = ImageIO.read(new File(outputImagePath));
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;

        double sum = 0;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int originalPixel = image.getRGB(i, j);
                int extractedPixel = outputImage.getRGB(i, j);

                int originalRed = (originalPixel >> 16) & 0xFF;
                int originalGreen = (originalPixel >> 8) & 0xFF;
                int originalBlue = originalPixel & 0xFF;

                int extractedRed = (extractedPixel >> 16) & 0xFF;
                int extractedGreen = (extractedPixel >> 8) & 0xFF;
                int extractedBlue = extractedPixel & 0xFF;

                double pixelDiffRed = Math.pow(originalRed - extractedRed, 2);
                double pixelDiffGreen = Math.pow(originalGreen - extractedGreen, 2);
                double pixelDiffBlue = Math.pow(originalBlue - extractedBlue, 2);

                sum += pixelDiffRed + pixelDiffGreen + pixelDiffBlue;
            }
        }

        double mse = sum / totalPixels;

        return mse;
    }


    private static int calculateMaxDeviation(String imagePath, String outputImagePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        BufferedImage outputImage = ImageIO.read(new File(outputImagePath));
        int width = image.getWidth();
        int height = image.getHeight();
        int maxDeviation = 0;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int originalPixel = image.getRGB(i, j);
                int extractedPixel = outputImage.getRGB(i, j);

                int originalRed = (originalPixel >> 16) & 0xFF;
                int originalGreen = (originalPixel >> 8) & 0xFF;
                int originalBlue = originalPixel & 0xFF;

                int extractedRed = (extractedPixel >> 16) & 0xFF;
                int extractedGreen = (extractedPixel >> 8) & 0xFF;
                int extractedBlue = extractedPixel & 0xFF;

                int deviationRed = Math.abs(originalRed - extractedRed);
                int deviationGreen = Math.abs(originalGreen - extractedGreen);
                int deviationBlue = Math.abs(originalBlue - extractedBlue);

                int maxPixelDeviation = Math.max(deviationRed, Math.max(deviationGreen, deviationBlue));

                maxDeviation = Math.max(maxDeviation, maxPixelDeviation);
            }
        }

        return maxDeviation;
    }

    private static void showStartMenu(String imagePath, String message, String outputImagePath) throws IOException {
        System.out.println("----- Лабораторная работа 1 (Алгоритм LSB) -----");
        System.out.println("Выполнил: Лавренко В.А., 10 группа, 3 курс");


        showMenu(imagePath, message, outputImagePath);


    }

    private static void showMenu(String imagePath, String message, String outputImagePath) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n");
        System.out.println("Что вы хотите сделать: ");
        System.out.println("1. Скрыть сообщение в изображении;");
        System.out.println("2. Достать сообщение из изображения;");
        System.out.println("3. Скрыть и достать сообщение ( + показать метрики);");
        System.out.println("4. Отобразить только метрики.");
        System.out.println("Введите число: ");
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                hideMessage(imagePath, message, outputImagePath);
                showMenu(imagePath, message, outputImagePath);
            case 2:
                String extractedMessage = extractMessage(outputImagePath);
                System.out.println("Извлечённое сообщение = " + extractedMessage);
                showMenu(imagePath, message, outputImagePath);
            case 3:
                hideMessage(imagePath, message, outputImagePath);
                String extractedMessage1 = extractMessage(outputImagePath);
                System.out.println("Извлечённое сообщение = " + extractedMessage1);
                // Норма Минковского
                double minkowskiNorm = calculateMinkowskiNorm(imagePath, outputImagePath);
                System.out.println("Норма Минковского = " + minkowskiNorm);

                // Среднее квадратичное отклонение MSE
                double mse = calculateMSE(imagePath, outputImagePath);
                System.out.println("Среднее квадратичное отклонение MSE = " + mse);

                // Максимальное абсолютное отклонение maxD
                int maxDeviation = calculateMaxDeviation(imagePath, outputImagePath);
                System.out.println("Максимальное абсолютное отклонение = " + maxDeviation);

                showMenu(imagePath, message, outputImagePath);
            case 4:
                // Норма Минковского
                double minkowskiNorm1 = calculateMinkowskiNorm(imagePath, outputImagePath);
                System.out.println("Норма Минковского = " + minkowskiNorm1);

                // Среднее квадратичное отклонение MSE
                double mse1 = calculateMSE(imagePath, outputImagePath);
                System.out.println("Среднее квадратичное отклонение MSE = " + mse1);

                // Максимальное абсолютное отклонение maxD
                int maxDeviation1 = calculateMaxDeviation(imagePath, outputImagePath);
                System.out.println("Максимальное абсолютное отклонение = " + maxDeviation1);
                showMenu(imagePath, message, outputImagePath);
            case 0:
                return;
            default:
                showMenu(imagePath, message, outputImagePath);

        }
    }
}