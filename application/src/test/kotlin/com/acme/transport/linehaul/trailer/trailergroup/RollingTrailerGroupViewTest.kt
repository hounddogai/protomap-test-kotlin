package com.acme.transport.linehaul.trailer.trailergroup

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingTrailerGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingTrailerGroup("code-840", "label-303")
        val view = toRollingTrailerGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingTrailerGroupViews(mutableListOf<RollingTrailerGroup>(item, item)).size, "views")
    }
}
