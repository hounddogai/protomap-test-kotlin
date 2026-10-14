package com.acme.transport.linehaul.laneplan.plancheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackLanePlanCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackLanePlanCheckpointStore()
        val item = newFallbackLanePlanCheckpoint("code-197", "title-550")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
