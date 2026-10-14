package com.acme.transport.crewing.qualification.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class QualificationMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newQualificationMapping("code-335", "name-195")
        val view = toQualificationMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toQualificationMappingViews(mutableListOf<QualificationMapping>(item, item)).size, "views")
    }
}
