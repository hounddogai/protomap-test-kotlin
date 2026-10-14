package com.acme.transport.lastmile.locker.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedLockerScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedLockerScoreStore()
        val item = newCappedLockerScore("code-549", "label-591")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
