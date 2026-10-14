package com.acme.transport.crewing.qualification.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryQualificationOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryQualificationOverride("code-926", "label-24")
        val view = toPrimaryQualificationOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryQualificationOverrideViews(mutableListOf<PrimaryQualificationOverride>(item, item)).size, "views")
    }
}
