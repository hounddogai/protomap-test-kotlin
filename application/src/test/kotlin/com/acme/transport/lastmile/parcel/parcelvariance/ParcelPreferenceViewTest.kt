package com.acme.transport.lastmile.parcel.parcelvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelPreference("code-249", "label-796")
        val view = toParcelPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelPreferenceViews(mutableListOf<ParcelPreference>(item, item)).size, "views")
    }
}
