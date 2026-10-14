package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalLeaseReturnOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalLeaseReturnOverrideEngine(newRegistry())
        val item = newGlobalLeaseReturnOverride("code-48", "label-713")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
