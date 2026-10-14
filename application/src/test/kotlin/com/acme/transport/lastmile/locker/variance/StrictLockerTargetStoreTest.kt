package com.acme.transport.lastmile.locker.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictLockerTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictLockerTargetStore()
        val item = newStrictLockerTarget("code-294", "title-993")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
