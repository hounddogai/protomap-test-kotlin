package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalQualificationWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalQualificationWindow("code-303", "name-703")
        val view = toRegionalQualificationWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalQualificationWindowViews(mutableListOf<RegionalQualificationWindow>(item, item)).size, "views")
    }
}
