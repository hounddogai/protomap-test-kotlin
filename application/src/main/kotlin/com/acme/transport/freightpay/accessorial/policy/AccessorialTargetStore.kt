package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial targets by code. */
interface AccessorialTargetStore {
    fun find(code: String): AccessorialTarget?
    fun save(item: AccessorialTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialTargetStore(
    var items: MutableMap<String, AccessorialTarget>,
    var order: MutableList<String>,
) : AccessorialTargetStore {
    override fun find(code: String): AccessorialTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialTarget) {
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

    override fun list(): MutableList<AccessorialTarget> {
        val out = mutableListOf<AccessorialTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialTargetStore(): AccessorialTargetStore {
    return MemoryAccessorialTargetStore(mutableMapOf<String, AccessorialTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialTargetStore(
    var inner: AccessorialTargetStore,
    var cache: TtlCache<AccessorialTarget>,
    var hits: Int,
    var misses: Int,
) : AccessorialTargetStore {
    override fun find(code: String): AccessorialTarget? {
        val hit = (cache.get(code) as AccessorialTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialTarget> = inner.list()
}

fun newCachedAccessorialTargetStore(inner: AccessorialTargetStore, ttl: Int, clock: Clock): AccessorialTargetStore {
    return CachedAccessorialTargetStore(
        inner = inner,
        cache = newTtlCache<AccessorialTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial targets. Empty fields match all. */
data class AccessorialTargetQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialTarget>): MutableList<AccessorialTarget> {
        val matched = filter(items) { candidate: AccessorialTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialTargets(store: AccessorialTargetStore, query: AccessorialTargetQuery): MutableList<AccessorialTarget> {
    return query.apply(store.list())
}
