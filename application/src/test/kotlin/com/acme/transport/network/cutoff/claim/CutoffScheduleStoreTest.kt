package com.acme.transport.network.cutoff.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffScheduleStore()
        val item = newCutoffSchedule("code-605", "title-565")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
