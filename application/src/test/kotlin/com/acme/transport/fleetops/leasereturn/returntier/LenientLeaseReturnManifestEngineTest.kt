package com.acme.transport.fleetops.leasereturn.returntier

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientLeaseReturnManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientLeaseReturnManifestEngine()
        val item = newLenientLeaseReturnManifest("code-553", "title-540")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
