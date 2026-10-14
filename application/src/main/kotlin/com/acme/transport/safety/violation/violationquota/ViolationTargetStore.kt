package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation targets by code. */
interface ViolationTargetStore {
    fun find(code: String): ViolationTarget?
    fun save(item: ViolationTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationTargetStore(
    var items: MutableMap<String, ViolationTarget>,
    var order: MutableList<String>,
) : ViolationTargetStore {
    override fun find(code: String): ViolationTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationTarget) {
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

    override fun list(): MutableList<ViolationTarget> {
        val out = mutableListOf<ViolationTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationTargetStore(): ViolationTargetStore {
    return MemoryViolationTargetStore(mutableMapOf<String, ViolationTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationTargetStore(
    var inner: ViolationTargetStore,
    var cache: TtlCache<ViolationTarget>,
    var hits: Int,
    var misses: Int,
) : ViolationTargetStore {
    override fun find(code: String): ViolationTarget? {
        val hit = (cache.get(code) as ViolationTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationTarget> = inner.list()
}

fun newCachedViolationTargetStore(inner: ViolationTargetStore, ttl: Int, clock: Clock): ViolationTargetStore {
    return CachedViolationTargetStore(
        inner = inner,
        cache = newTtlCache<ViolationTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation targets. Empty fields match all. */
data class ViolationTargetQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationTarget>): MutableList<ViolationTarget> {
        val matched = filter(items) { candidate: ViolationTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationTargets(store: ViolationTargetStore, query: ViolationTargetQuery): MutableList<ViolationTarget> {
    return query.apply(store.list())
}
