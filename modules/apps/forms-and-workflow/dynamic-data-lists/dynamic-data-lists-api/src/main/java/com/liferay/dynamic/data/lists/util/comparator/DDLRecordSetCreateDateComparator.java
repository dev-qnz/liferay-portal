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

package com.liferay.dynamic.data.lists.util.comparator;

import com.liferay.dynamic.data.lists.model.DDLRecordSet;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.OrderByComparator;

/**
 * Used to order record sets according to their create date during search
 * operations. The order can be ascending or descending and is defined by the
 * value specified in the class constructor.
 *
 * @author Rafael Praxedes
 * @see    com.liferay.dynamic.data.lists.service.DDLRecordSetService#search(
 *         long, long, String, int, int, int, OrderByComparator)
 */
public class DDLRecordSetCreateDateComparator
	extends OrderByComparator<DDLRecordSet> {

	public static final String ORDER_BY_ASC = "DDLRecordSet.createDate ASC";

	public static final String ORDER_BY_DESC = "DDLRecordSet.createDate DESC";

	public static final String[] ORDER_BY_FIELDS = {"createDate"};

	public DDLRecordSetCreateDateComparator() {
		this(false);
	}

	public DDLRecordSetCreateDateComparator(boolean ascending) {
		_ascending = ascending;
	}

	@Override
	public int compare(DDLRecordSet ddlRecordSet1, DDLRecordSet ddlRecordSet2) {
		int value = DateUtil.compareTo(
			ddlRecordSet1.getCreateDate(), ddlRecordSet2.getCreateDate());

		if (_ascending) {
			return value;
		}
		else {
			return -value;
		}
	}

	@Override
	public String getOrderBy() {
		if (_ascending) {
			return ORDER_BY_ASC;
		}
		else {
			return ORDER_BY_DESC;
		}
	}

	@Override
	public String[] getOrderByFields() {
		return ORDER_BY_FIELDS;
	}

	@Override
	public boolean isAscending() {
		return _ascending;
	}

	private final boolean _ascending;

}