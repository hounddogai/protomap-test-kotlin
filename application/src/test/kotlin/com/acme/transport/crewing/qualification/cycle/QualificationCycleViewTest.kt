package com.acme.transport.crewing.qualification.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class QualificationCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newQualificationCycle("code-217", "title-291")
        val view = toQualificationCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toQualificationCycleViews(mutableListOf<QualificationCycle>(item, item)).size, "views")
    }
}
