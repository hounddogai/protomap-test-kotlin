package com.acme.transport.linehaul.relay.relayquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling relay requests by code. */
interface RollingRelayRequestStore {
    fun find(code: String): RollingRelayRequest?
    fun save(item: RollingRelayRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRelayRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRelayRequestStore(
    var items: MutableMap<String, RollingRelayRequest>,
    var order: MutableList<String>,
) : RollingRelayRequestStore {
    override fun find(code: String): RollingRelayRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRelayRequest) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<RollingRelayRequest> {
        val out = mutableListOf<RollingRelayRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRelayRequestStore(): RollingRelayRequestStore {
    return MemoryRollingRelayRequestStore(mutableMapOf<String, RollingRelayRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRelayRequestStore(
    var inner: RollingRelayRequestStore,
    var cache: TtlCache<RollingRelayRequest>,
    var hits: Int,
    var misses: Int,
) : RollingRelayRequestStore {
    override fun find(code: String): RollingRelayRequest? {
        val hit = (cache.get(code) as RollingRelayRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRelayRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRelayRequest> = inner.list()
}

fun newCachedRollingRelayRequestStore(inner: RollingRelayRequestStore, ttl: Int, clock: Clock): RollingRelayRequestStore {
    return CachedRollingRelayRequestStore(
        inner = inner,
        cache = newTtlCache<RollingRelayRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling relay requests. Empty fields match all. */
data class RollingRelayRequestQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRelayRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingRelayRequest>): MutableList<RollingRelayRequest> {
        val matched = filter(items) { candidate: RollingRelayRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRelayRequests(store: RollingRelayRequestStore, query: RollingRelayRequestQuery): MutableList<RollingRelayRequest> {
    return query.apply(store.list())
}
