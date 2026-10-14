package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingTenderScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingTenderScoreStore()
        val item = newPendingTenderScore("code-644", "name-93")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
