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

package com.liferay.portal.cluster.multiple.sample.internal;

import com.liferay.portal.kernel.cluster.ClusterExecutorUtil;
import com.liferay.portal.kernel.cluster.ClusterNode;

import java.io.Serializable;

/**
 * @author Tina Tian
 */
public class ClusterSampleClass implements Serializable {

	public static int getPortalLocalPort() {
		ClusterNode clusterNode = ClusterExecutorUtil.getLocalClusterNode();

		return clusterNode.getPortalPort();
	}

	public ClusterSampleClass(String name) {
		_name = name;
	}

	@Override
	public String toString() {
		return _name;
	}

	private final String _name;

}