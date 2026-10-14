package com.acme.transport.crewing.qualification.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class QualificationPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newQualificationPolicy("code-290", "label-118")
        val view = toQualificationPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toQualificationPolicyViews(mutableListOf<QualificationPolicy>(item, item)).size, "views")
    }
}
