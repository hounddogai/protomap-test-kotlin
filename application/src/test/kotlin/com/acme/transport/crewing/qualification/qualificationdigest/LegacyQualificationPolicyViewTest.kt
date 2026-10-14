package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyQualificationPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyQualificationPolicy("code-919", "name-260")
        val view = toLegacyQualificationPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyQualificationPolicyViews(mutableListOf<LegacyQualificationPolicy>(item, item)).size, "views")
    }
}
