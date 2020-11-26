/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.dynamic.data.mapping.form.web.internal.upload;

import com.liferay.document.library.kernel.exception.FileExtensionException;
import com.liferay.document.library.kernel.exception.FileNameException;
import com.liferay.document.library.kernel.exception.FileSizeException;
import com.liferay.dynamic.data.mapping.form.web.internal.configuration.DDMFormWebConfiguration;
import com.liferay.dynamic.data.mapping.form.web.internal.configuration.activator.DDMFormWebConfigurationActivator;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.util.FileImpl;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberMatcher;

/**
 * @author Carolina Barbosa
 */
public class DDMFormUploadValidatorTest {

	@Before
	public void setUp() throws Exception {
		setUpDDMFormWebConfigurationActivator();
		setUpFileUtil();
	}

	@Test(expected = FileExtensionException.class)
	public void testInvalidFileExtension() throws Exception {
		_ddmFormUploadValidator.validateFileExtension("test.xml");
	}

	@Test(expected = FileNameException.class)
	public void testInvalidFileName() throws Exception {
		_ddmFormUploadValidator.validateFileName("test.pdf.jpg");
	}

	@Test(expected = FileNameException.class)
	public void testInvalidFileName2() throws Exception {
		_ddmFormUploadValidator.validateFileName("test/.jpg");
	}

	@Test(expected = FileSizeException.class)
	public void testInvalidFileSize() throws Exception {
		_ddmFormUploadValidator.validateFileSize(mockFile(26), "test.jpg");
	}

	@Test
	public void testValidFileExtension() throws Exception {
		_ddmFormUploadValidator.validateFileExtension("test.JpG");
	}

	@Test
	public void testValidFileName() throws Exception {
		_ddmFormUploadValidator.validateFileName("Test_Upload - Forms.jpg");
	}

	@Test
	public void testValidFileSize() throws Exception {
		_ddmFormUploadValidator.validateFileSize(mockFile(24), "test.jpg");
	}

	protected File mockFile(long length) {
		File file = Mockito.mock(File.class);

		PowerMockito.when(
			file.length()
		).thenReturn(
			length * _FILE_LENGTH_MB
		);

		return file;
	}

	protected void setUpDDMFormWebConfigurationActivator() throws Exception {
		DDMFormWebConfigurationActivator ddmFormWebConfigurationActivator =
			Mockito.mock(DDMFormWebConfigurationActivator.class);

		MemberMatcher.field(
			DDMFormUploadValidator.class, "_ddmFormWebConfigurationActivator"
		).set(
			_ddmFormUploadValidator, ddmFormWebConfigurationActivator
		);

		DDMFormWebConfiguration ddmFormWebConfiguration =
			ConfigurableUtil.createConfigurable(
				DDMFormWebConfiguration.class, new HashMapDictionary<>());

		PowerMockito.when(
			ddmFormWebConfigurationActivator.getDDMFormWebConfiguration()
		).thenReturn(
			ddmFormWebConfiguration
		);
	}

	protected void setUpFileUtil() {
		FileUtil fileUtil = new FileUtil();

		fileUtil.setFile(FileImpl.getInstance());
	}

	private static final long _FILE_LENGTH_MB = 1024 * 1024;

	private final DDMFormUploadValidator _ddmFormUploadValidator =
		new DDMFormUploadValidator();

}