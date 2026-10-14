package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictRouteOffsetCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictRouteOffsetCheckpoint("code-743", "title-302")
        val view = toStrictRouteOffsetCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictRouteOffsetCheckpointViews(mutableListOf<StrictRouteOffsetCheckpoint>(item, item)).size, "views")
    }
}
