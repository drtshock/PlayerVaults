/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package com.drtshock.playervaults.lib.com.typesafe.config.impl;

import com.drtshock.playervaults.lib.com.typesafe.config.ConfigIncluder;
import com.drtshock.playervaults.lib.com.typesafe.config.ConfigIncluderClasspath;
import com.drtshock.playervaults.lib.com.typesafe.config.ConfigIncluderFile;
import com.drtshock.playervaults.lib.com.typesafe.config.ConfigIncluderURL;

interface FullIncluder extends ConfigIncluder, ConfigIncluderFile, ConfigIncluderURL,
            ConfigIncluderClasspath {

}
