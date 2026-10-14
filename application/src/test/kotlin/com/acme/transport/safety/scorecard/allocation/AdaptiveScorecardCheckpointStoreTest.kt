package com.acme.transport.safety.scorecard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveScorecardCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveScorecardCheckpointStore()
        val item = newAdaptiveScorecardCheckpoint("code-639", "title-667")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
