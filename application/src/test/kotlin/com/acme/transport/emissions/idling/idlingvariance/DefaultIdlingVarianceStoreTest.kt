package com.acme.transport.emissions.idling.idlingvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultIdlingVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultIdlingVarianceStore()
        val item = newDefaultIdlingVariance("code-717", "name-511")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
