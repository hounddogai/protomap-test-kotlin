package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals

class QualificationWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newQualificationWindow("code-166", "name-682")
        val view = toQualificationWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toQualificationWindowViews(mutableListOf<QualificationWindow>(item, item)).size, "views")
    }
}
