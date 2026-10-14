package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial tiers by code. */
interface AccessorialTierStore {
    fun find(code: String): AccessorialTier?
    fun save(item: AccessorialTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialTierStore(
    var items: MutableMap<String, AccessorialTier>,
    var order: MutableList<String>,
) : AccessorialTierStore {
    override fun find(code: String): AccessorialTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialTier) {
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

    override fun list(): MutableList<AccessorialTier> {
        val out = mutableListOf<AccessorialTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialTierStore(): AccessorialTierStore {
    return MemoryAccessorialTierStore(mutableMapOf<String, AccessorialTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialTierStore(
    var inner: AccessorialTierStore,
    var cache: TtlCache<AccessorialTier>,
    var hits: Int,
    var misses: Int,
) : AccessorialTierStore {
    override fun find(code: String): AccessorialTier? {
        val hit = (cache.get(code) as AccessorialTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialTier> = inner.list()
}

fun newCachedAccessorialTierStore(inner: AccessorialTierStore, ttl: Int, clock: Clock): AccessorialTierStore {
    return CachedAccessorialTierStore(
        inner = inner,
        cache = newTtlCache<AccessorialTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial tiers. Empty fields match all. */
data class AccessorialTierQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialTier): Boolean {
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

    fun apply(items: MutableList<AccessorialTier>): MutableList<AccessorialTier> {
        val matched = filter(items) { candidate: AccessorialTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialTiers(store: AccessorialTierStore, query: AccessorialTierQuery): MutableList<AccessorialTier> {
    return query.apply(store.list())
}
