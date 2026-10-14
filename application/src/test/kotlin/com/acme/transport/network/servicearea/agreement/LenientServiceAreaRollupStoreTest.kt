package com.acme.transport.network.servicearea.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientServiceAreaRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientServiceAreaRollupStore()
        val item = newLenientServiceAreaRollup("code-840", "title-432")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
