package com.acme.transport.crewing.qualification.qualificationoverride

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveQualificationOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveQualificationOverride("code-230", "title-303")
        val view = toAdaptiveQualificationOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveQualificationOverrideViews(mutableListOf<AdaptiveQualificationOverride>(item, item)).size, "views")
    }
}
