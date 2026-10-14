package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalFuelBurnDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalFuelBurnDigestStore()
        val item = newGlobalFuelBurnDigest("code-402", "label-596")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
