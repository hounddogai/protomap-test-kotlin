package com.acme.transport.linehaul.backhaul.backhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulScoreStore()
        val item = newBackhaulScore("code-531", "label-890")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
