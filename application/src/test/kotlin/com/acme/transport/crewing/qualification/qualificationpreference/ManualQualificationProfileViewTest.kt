package com.acme.transport.crewing.qualification.qualificationpreference

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualQualificationProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualQualificationProfile("code-598", "title-229")
        val view = toManualQualificationProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualQualificationProfileViews(mutableListOf<ManualQualificationProfile>(item, item)).size, "views")
    }
}
