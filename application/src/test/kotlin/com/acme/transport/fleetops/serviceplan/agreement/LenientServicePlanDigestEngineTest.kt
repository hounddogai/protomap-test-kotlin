package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientServicePlanDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientServicePlanDigestEngine(newRegistry())
        val item = newLenientServicePlanDigest("code-195", "title-759")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
