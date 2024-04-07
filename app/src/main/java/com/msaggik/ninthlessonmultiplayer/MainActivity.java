package com.msaggik.ninthlessonmultiplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // поля
    private RadioButton inputFileRaw, inputFileAssets, inputInternet; // кнопки задания источника аудио
    private FloatingActionButton fabPlayPause, fabBack, fabForward; // кнопки управления воспроизведением
    private MediaPlayer mediaPlayer; // поле медиа-плеера
    private final String URI_STREAM = "https://radio.azbyka.ru/lives"; // поле константы URI ссылки (например: https://radio.azbyka.ru/lives)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // привязка полей к разметке
        inputFileRaw = findViewById(R.id.input_file_raw);
        inputFileAssets = findViewById(R.id.input_file_assets);
        inputInternet = findViewById(R.id.input_internet);
        fabPlayPause = findViewById(R.id.fab_play_pause);
        fabBack = findViewById(R.id.fab_back);
        fabForward = findViewById(R.id.fab_forward);

        // обработка нажатия кнопок
        inputFileRaw.setOnClickListener(listener);
        inputFileAssets.setOnClickListener(listener);
        inputInternet.setOnClickListener(listener);
        fabPlayPause.setOnClickListener(listener);
        fabBack.setOnClickListener(listener);
        fabForward.setOnClickListener(listener);
    }

    // создадим один слушатель на все кнопки
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.input_file_raw:
                    // код воспроизведения файла
                    releasePlayer(); // очистка памяти прошлого воспроизведения
                    Toast.makeText(MainActivity.this, "Воспроизведение аудио-файла из папки Raw", Toast.LENGTH_SHORT).show();
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.flight_of_the_bumblebee); // загрузка аудио-файла в медиа-плеер
                    // видимость кнопок
                    fabBack.setVisibility(View.VISIBLE);
                    fabForward.setVisibility(View.VISIBLE);
                    break;
                case R.id.input_file_assets:
                    // код воспроизведения файла
                    releasePlayer(); // очистка памяти прошлого воспроизведения
                    mediaPlayer = new MediaPlayer(); // создание объекта медиа-плеера
                    Toast.makeText(MainActivity.this, "Воспроизведение аудио-файла из папки Assets", Toast.LENGTH_SHORT).show();
                    // альтернативный способ считывания файла с помощью файлового дескриптора
                    AssetFileDescriptor descriptor = null;
                    try {
                        descriptor = getAssets().openFd("Н.А.Римский-Корсаков - Полёт шмеля.mp3");
                        // запись файла в mediaPlayer, задаются параметры (путь файла, смещение относительно начала файла, длина аудио в файле)
                        mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                        descriptor.close(); // закрытие дескриптора
                        mediaPlayer.prepare(); // ассинхронная подготовка плейера к проигрыванию
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "В папке Assets необходимого файла не нашлось", Toast.LENGTH_SHORT).show();
                    }
                    // видимость кнопок
                    fabBack.setVisibility(View.VISIBLE);
                    fabForward.setVisibility(View.VISIBLE);
                    break;
                case R.id.input_internet:
                    // код воспроизведения потока из интернета
                    releasePlayer(); // очистка памяти прошлого воспроизведения
                    Toast.makeText(MainActivity.this, "Воспроизведение потока из интернета", Toast.LENGTH_SHORT).show();
                    mediaPlayer = new MediaPlayer(); // создание объекта медиа-плеера
                    try {
                        mediaPlayer.setDataSource(URI_STREAM); // загрузка URI источника потока аудио в интернете
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "Отсутствует рабочая URI ссылка", Toast.LENGTH_SHORT).show();
                    }
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // определение типа потока
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {  // задание слушателя воспроизведения аудио-потока
                        // данный метод вызывается когда плеер готов к проигрыванию
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            Toast.makeText(MainActivity.this, "Плеер готов к воспроизведению", Toast.LENGTH_SHORT).show();
                            mediaPlayer.start();
                        }
                    });
                    mediaPlayer.prepareAsync(); // ассинхронное воспроизведение аудио в дополнительном потоке
                    // видимость кнопок
                    fabBack.setVisibility(View.INVISIBLE);
                    fabForward.setVisibility(View.INVISIBLE);
                    break;
                case R.id.fab_play_pause:
                    // код старта и паузы
                    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                        // назначение кнопке картинки паузы
                        fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
                        mediaPlayer.start();
                    } else if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        // назначение кнопке картинки воспроизведения
                        fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                        mediaPlayer.pause();
                    }
                    break;
                case R.id.fab_back:
                    // код перемотки назад
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000); // перемотка назад на 5 секунд
                    }
                    break;
                case R.id.fab_forward:
                    // код перемотки вперёд
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000); // перемотка вперёд на 5 секунд
                    }
                    break;
            }
        }
    };

    // метод очистки занятой аудио-плеером памяти
    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release(); // очистка памяти
            mediaPlayer = null; // обнуление объекта аудио-плеера
        }
    }

    @Override
    protected void onDestroy() {
        releasePlayer(); // очистка памяти прошлого воспроизведения
        super.onDestroy();
    }
}