package com.acme.transport.linehaul.trailer.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingTrailerCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingTrailerCheckpoint("code-141", "name-532")
        val view = toRollingTrailerCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingTrailerCheckpointViews(mutableListOf<RollingTrailerCheckpoint>(item, item)).size, "views")
    }
}
