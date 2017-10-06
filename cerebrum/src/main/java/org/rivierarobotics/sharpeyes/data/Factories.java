/*
 * This file is part of cerebrum, licensed under the MIT License (MIT).
 *
 * Copyright (c) Team5818 <https://github.com/Team5818/SharpEyes>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.rivierarobotics.sharpeyes.data;

import org.rivierarobotics.sharpeyes.data.file.InteractiveFileDataProvider;
import org.rivierarobotics.sharpeyes.data.qrvid.QrVidDataProvider;

import com.google.auto.service.AutoService;

import javafx.stage.Window;

class Factories {

    @AutoService(DataProviderFactory.class)
    public static final class FileDPF implements DataProviderFactory {

        @Override
        public DataProvider create(Window parentWindow) {
            return new InteractiveFileDataProvider(parentWindow);
        }

        @Override
        public String getId() {
            return "data.provider.file";
        }

    }

    // @AutoService(DataProviderFactory.class)
    public static final class QrVidDPF implements DataProviderFactory {

        @Override
        public DataProvider create(Window parentWindow) {
            return new QrVidDataProvider();
        }

        @Override
        public String getId() {
            return "data.provider.qrvid";
        }

    }

}
