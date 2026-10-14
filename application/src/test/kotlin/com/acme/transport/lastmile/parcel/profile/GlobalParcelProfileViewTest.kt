package com.acme.transport.lastmile.parcel.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalParcelProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalParcelProfile("code-247", "label-600")
        val view = toGlobalParcelProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalParcelProfileViews(mutableListOf<GlobalParcelProfile>(item, item)).size, "views")
    }
}
