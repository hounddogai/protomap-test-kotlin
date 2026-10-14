package com.acme.transport.crewing.qualification.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalQualificationBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalQualificationBundle("code-555", "name-306")
        val view = toGlobalQualificationBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalQualificationBundleViews(mutableListOf<GlobalQualificationBundle>(item, item)).size, "views")
    }
}
