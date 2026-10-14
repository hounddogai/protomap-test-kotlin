package com.acme.transport.crewing.qualification.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class QualificationOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newQualificationOverride("code-76", "title-188")
        val view = toQualificationOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toQualificationOverrideViews(mutableListOf<QualificationOverride>(item, item)).size, "views")
    }
}
