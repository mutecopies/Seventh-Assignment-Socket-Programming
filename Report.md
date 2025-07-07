
## 1. Three Ways to Send a Login Message

This report discusses three different methods for sending login credentials over a socket connection using Java:

- Plain String  
- Serialized Java Object  
- JSON  

Each method is implemented in the provided `Client` class, and we analyze their respective pros, cons, and suitability for communication.

---

## Method 1: Plain String Format

**Example Sent Message:**

```java
stringOut.println("LOGIN|user1|pass123");
```

### ✅ Pros

- **Simplicity**: Easy to implement and understand.  
- **Lightweight**: Minimal data overhead.  
- **No external libraries** required.  

### ❌ Cons

- **Parsing Errors**: Manual parsing required using a delimiter like `|`, which can be problematic if the delimiter appears in the actual data.  
- **No Type Safety**: Everything is text-based.  
- **Limited Structure**: Doesn’t scale for complex/nested data.  
- **Error-prone**: Fragile and difficult to maintain.  

### 🔍 Parsing Strategy

```java
String message = "LOGIN|user1|pass123";
String[] parts = message.split("\\|");
String command = parts[0];  // LOGIN
String username = parts[1]; // user1
String password = parts[2]; // pass123
```

### ⚠️ Edge Case

If a password like `pa|ss123` is used, the delimiter interferes with parsing.

### 🚫 Complex Data Support

Not suitable for nested data or more complex structures.

---

## Method 2: Serialized Java Object

**Example Sent Message:**

```java
ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
objectOut.writeObject(loginRequest);
```

### ✅ Pros

- **Type Safety**: Sends full Java object with structure preserved.  
- **No Manual Parsing**: Deserialization returns the original object.  
- **Supports Complex Data**: Nested structures are preserved.  

### ❌ Cons

- **Java-only**: Cannot be understood by clients written in other languages.  
- **Version Coupling**: Both client and server must have compatible class definitions.  
- **Security Risks**: Vulnerable to deserialization attacks if not handled carefully.  
- **Overhead**: More verbose than other formats.  

### 🌐 Interoperability

❌ **Not suitable** for communication with Python, JavaScript, etc.

---

## Method 3: JSON

### Why is JSON often preferred for communication between different systems?

JSON (JavaScript Object Notation) is a lightweight, text-based data format that is easy for both humans and machines to read and write. It is language-independent but uses conventions familiar to programmers of the C-family of languages, including Java, JavaScript, Python, and many others. JSON is preferred because:

- It is **easy to serialize and deserialize**.
- It supports **structured and nested data**.
- It is **widely supported across many languages** with built-in libraries.
- It is **text-based**, which makes debugging and logging easier.
- It is a **standard format** for APIs and data exchange.

### Would this format work with servers or clients written in other languages?

✅ **Yes**. JSON is specifically designed to be **cross-platform** and **language-agnostic**. You can send JSON from a Java client and parse it easily in:

- Python (`json.loads`)
- JavaScript (`JSON.parse`)
- C# (`System.Text.Json`)
- Go (`encoding/json`)
- PHP (`json_decode`)

This makes JSON ideal for systems that need to communicate across different platforms and languages.
