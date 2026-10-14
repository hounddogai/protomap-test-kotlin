package com.acme.transport.lastmile.locker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultLockerDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultLockerDigestStore()
        val item = newDefaultLockerDigest("code-142", "name-750")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
