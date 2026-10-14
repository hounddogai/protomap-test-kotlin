package com.acme.transport.freightpay.accessorial.quota

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial profiles by code. */
interface AccessorialProfileStore {
    fun find(code: String): AccessorialProfile?
    fun save(item: AccessorialProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialProfileStore(
    var items: MutableMap<String, AccessorialProfile>,
    var order: MutableList<String>,
) : AccessorialProfileStore {
    override fun find(code: String): AccessorialProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialProfile) {
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

    override fun list(): MutableList<AccessorialProfile> {
        val out = mutableListOf<AccessorialProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialProfileStore(): AccessorialProfileStore {
    return MemoryAccessorialProfileStore(mutableMapOf<String, AccessorialProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialProfileStore(
    var inner: AccessorialProfileStore,
    var cache: TtlCache<AccessorialProfile>,
    var hits: Int,
    var misses: Int,
) : AccessorialProfileStore {
    override fun find(code: String): AccessorialProfile? {
        val hit = (cache.get(code) as AccessorialProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialProfile> = inner.list()
}

fun newCachedAccessorialProfileStore(inner: AccessorialProfileStore, ttl: Int, clock: Clock): AccessorialProfileStore {
    return CachedAccessorialProfileStore(
        inner = inner,
        cache = newTtlCache<AccessorialProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial profiles. Empty fields match all. */
data class AccessorialProfileQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<AccessorialProfile>): MutableList<AccessorialProfile> {
        val matched = filter(items) { candidate: AccessorialProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialProfiles(store: AccessorialProfileStore, query: AccessorialProfileQuery): MutableList<AccessorialProfile> {
    return query.apply(store.list())
}
