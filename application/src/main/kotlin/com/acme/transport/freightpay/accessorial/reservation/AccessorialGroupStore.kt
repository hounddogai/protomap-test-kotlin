package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial groups by code. */
interface AccessorialGroupStore {
    fun find(code: String): AccessorialGroup?
    fun save(item: AccessorialGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialGroupStore(
    var items: MutableMap<String, AccessorialGroup>,
    var order: MutableList<String>,
) : AccessorialGroupStore {
    override fun find(code: String): AccessorialGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialGroup) {
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

    override fun list(): MutableList<AccessorialGroup> {
        val out = mutableListOf<AccessorialGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialGroupStore(): AccessorialGroupStore {
    return MemoryAccessorialGroupStore(mutableMapOf<String, AccessorialGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialGroupStore(
    var inner: AccessorialGroupStore,
    var cache: TtlCache<AccessorialGroup>,
    var hits: Int,
    var misses: Int,
) : AccessorialGroupStore {
    override fun find(code: String): AccessorialGroup? {
        val hit = (cache.get(code) as AccessorialGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialGroup> = inner.list()
}

fun newCachedAccessorialGroupStore(inner: AccessorialGroupStore, ttl: Int, clock: Clock): AccessorialGroupStore {
    return CachedAccessorialGroupStore(
        inner = inner,
        cache = newTtlCache<AccessorialGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial groups. Empty fields match all. */
data class AccessorialGroupQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialGroup>): MutableList<AccessorialGroup> {
        val matched = filter(items) { candidate: AccessorialGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialGroups(store: AccessorialGroupStore, query: AccessorialGroupQuery): MutableList<AccessorialGroup> {
    return query.apply(store.list())
}
