package com.acme.transport.linehaul.tender.template

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryTenderPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryTenderPreference("code-317", "title-58")
        val view = toPrimaryTenderPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryTenderPreferenceViews(mutableListOf<PrimaryTenderPreference>(item, item)).size, "views")
    }
}
