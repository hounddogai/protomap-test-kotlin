package com.acme.transport.terminal.dwell.dwellcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDwellDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDwellDigest("code-577", "label-408")
        val view = toRegionalDwellDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDwellDigestViews(mutableListOf<RegionalDwellDigest>(item, item)).size, "views")
    }
}
