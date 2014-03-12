DroidKit
========

Android Dev. Kit

Install
========
With gradle:

    repositories {
        maven { url 'https://raw.github.com/elegion/maven/master/' }
    }

    dependencies {
        compile 'com.lightydev:droidkit:+'
    }

Async Image Loading
========

    DkImageView image = (DkImageView) findViewById(R.id.dk_image);
    image.loadImage(url);

Async Network
========

    Http.get(url).setCallback(new AsyncHttpCallback() {
        @Override
        public void onSuccess(final int statusCode, final Map<String, String> headers, final InputStream content) {
            //parse content
        }
        @Override
        public void onError(final HttpException e) {
            //handle error
        }
    }).send();

## License

	Copyright 2013-2014 Daniel Serdyukov

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
