package com.acme.transport.terminal.dwell.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDwellCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDwellCheckpoint("code-950", "label-423")
        val view = toLenientDwellCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDwellCheckpointViews(mutableListOf<LenientDwellCheckpoint>(item, item)).size, "views")
    }
}
