package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryLockerTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryLockerTargetStore()
        val item = newPrimaryLockerTarget("code-196", "label-517")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
