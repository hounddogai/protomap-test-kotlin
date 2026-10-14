package com.acme.transport.crewing.qualification.qualificationmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualQualificationThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualQualificationThreshold("code-384", "name-90")
        val view = toManualQualificationThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualQualificationThresholdViews(mutableListOf<ManualQualificationThreshold>(item, item)).size, "views")
    }
}
