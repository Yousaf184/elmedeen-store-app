CREATE TABLE storeApps(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    app_name_eng VARCHAR(255) NOT NULL UNIQUE,
    app_name_urd VARCHAR(255) NOT NULL UNIQUE,
    app_name_arb VARCHAR(255) NOT NULL UNIQUE,
    app_description_eng VARCHAR(255) NOT NULL,
    app_description_urd VARCHAR(255) NOT NULL,
    app_description_arb VARCHAR(255) NOT NULL,
    app_apk_size VARCHAR(50) NOT NULL,
    app_package_name VARCHAR(255) NOT NULL UNIQUE,
    app_title_image_url VARCHAR(255) NOT NULL UNIQUE,
    app_version_url VARCHAR(255) NOT NULL UNIQUE,
    app_download_url VARCHAR(255) NOT NULL UNIQUE,
    app_screenshot1_url VARCHAR(255) NOT NULL UNIQUE,
    app_screenshot2_url VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO storeapps (
	app_name_eng,
	app_name_urd,
	app_name_arb,
	app_description_eng,
	app_description_urd,
	app_description_arb,
    app_apk_size,
	app_package_name,
	app_title_image_url,
	app_version_url,
	app_download_url,
	app_screenshot1_url,
	app_screenshot2_url
)
VALUES ('App 1', 'App 1', 'App 1', 'This is test app 1', 'This is test app 1', 'This is test app 1', '1.62 MB', 'com.example.tayyabbutt.app1', 'http://www.elmedeen.com/mobile/elmedeen_app_store/app1/app_title_image.png', 
'http://www.elmedeen.com/mobile/elmedeen_app_store/app1/app_version.txt', 'http://www.elmedeen.com/mobile/elmedeen_app_store/app1/app1.apk', 'http://www.elmedeen.com/mobile/elmedeen_app_store/app1/screenshot1.png', 'http://www.elmedeen.com/mobile/elmedeen_app_store/app1/screenshot2.png'),
('App 2', 'App 2', 'App 2', 'This is test app 2', 'This is test app 2', 'This is test app 2', '1.63 MB', 'com.example.tayyabbutt.app2', 'http://www.elmedeen.com/mobile/elmedeen_app_store/app2/app_title_image.png', 
'http://www.elmedeen.com/mobile/elmedeen_app_store/app2/app_version.txt', 'http://www.elmedeen.com/mobile/elmedeen_app_store/app2/app2.apk', 'http://www.elmedeen.com/mobile/elmedeen_app_store/app2/screenshot1.png', 'http://www.elmedeen.com/mobile/elmedeen_app_store/app2/screenshot2.png'),
('App 3', 'App 3', 'App 3', 'This is test app 3', 'This is test app 3', 'This is test app 3', '1.64 MB', 'com.example.tayyabbutt.app3', 'http://www.elmedeen.com/mobile/elmedeen_app_store/app3/app_title_image.png', 
'http://www.elmedeen.com/mobile/elmedeen_app_store/app3/app_version.txt', 'http://www.elmedeen.com/mobile/elmedeen_app_store/app3/app3.apk', 'http://www.elmedeen.com/mobile/elmedeen_app_store/app3/screenshot1.png', 'http://www.elmedeen.com/mobile/elmedeen_app_store/app3/screenshot2.png');