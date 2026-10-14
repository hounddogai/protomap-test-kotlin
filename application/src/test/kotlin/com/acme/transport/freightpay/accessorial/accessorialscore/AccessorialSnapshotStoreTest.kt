package com.acme.transport.freightpay.accessorial.accessorialscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialSnapshotStore()
        val item = newAccessorialSnapshot("code-275", "title-860")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
