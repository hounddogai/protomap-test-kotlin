package com.acme.transport.crewing.qualification.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalQualificationPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalQualificationPolicy("code-770", "title-978")
        val view = toGlobalQualificationPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalQualificationPolicyViews(mutableListOf<GlobalQualificationPolicy>(item, item)).size, "views")
    }
}
