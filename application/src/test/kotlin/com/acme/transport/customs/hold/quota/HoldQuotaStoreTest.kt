package com.acme.transport.customs.hold.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldQuotaStore()
        val item = newHoldQuota("code-858", "name-787")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
