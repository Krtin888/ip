# UI Test Plan

Each case is run in a fresh Chris process. Expected output is compared exactly, including separators, spaces, and line breaks.

## TC-1 Task lifecycle

**Aim:** Verify creation, listing, marking, unmarking, arbitrary date text, and exit behavior.

### Input

```text
todo read book
deadline return book /by no idea :-p
event project meeting /from Mon 2pm /to 4pm
mark 2
unmark 2
list
bye
```

### Expected output

```text
____________________________________________________________
 Hello! I'm Chris
 What can I do for you?
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] read book
 Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [D][ ] return book (by: no idea :-p)
 Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [E][ ] project meeting (from: Mon 2pm to: 4pm)
 Now you have 3 tasks in the list.
____________________________________________________________
____________________________________________________________
 Nice! I've marked this task as done:
   [D][X] return book (by: no idea :-p)
____________________________________________________________
____________________________________________________________
 OK, I've marked this task as not done yet:
   [D][ ] return book (by: no idea :-p)
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
 1.[T][ ] read book
 2.[D][ ] return book (by: no idea :-p)
 3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
____________________________________________________________
____________________________________________________________
 Bye. Hope to see you again soon!
____________________________________________________________
```

## TC-2 Invalid inputs preserve state

**Aim:** Verify malformed commands are explained and do not add or modify tasks.

### Input

```text
todo
blah
todo valid task
deadline missing date
event meeting /from 2pm
mark two
mark 9
list
bye
```

### Expected output

```text
____________________________________________________________
 Hello! I'm Chris
 What can I do for you?
____________________________________________________________
____________________________________________________________
 OOPS!!! A todo needs a description, e.g., todo read book.
____________________________________________________________
____________________________________________________________
 OOPS!!! I don't recognise that command. Try todo, deadline, event, list, mark, unmark, delete, or bye.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] valid task
 Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
 OOPS!!! A deadline needs '/by', e.g., deadline return book /by Sunday.
____________________________________________________________
____________________________________________________________
 OOPS!!! An event needs '/from' and '/to' times.
____________________________________________________________
____________________________________________________________
 OOPS!!! The task number must be a whole number.
____________________________________________________________
____________________________________________________________
 OOPS!!! Task 9 is not in the list.
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
 1.[T][ ] valid task
____________________________________________________________
____________________________________________________________
 Bye. Hope to see you again soon!
____________________________________________________________
```

## TC-3 Delete tasks

**Aim:** Verify deletion removes the selected task, shifts numbering, reports the new count, and rejects invalid task numbers.

### Input

```text
todo first task
deadline second task /by tomorrow
event third task /from 2pm /to 3pm
delete 2
list
delete two
delete 9
bye
```

### Expected output

```text
____________________________________________________________
 Hello! I'm Chris
 What can I do for you?
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] first task
 Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [D][ ] second task (by: tomorrow)
 Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [E][ ] third task (from: 2pm to: 3pm)
 Now you have 3 tasks in the list.
____________________________________________________________
____________________________________________________________
 Noted. I've removed this task:
   [D][ ] second task (by: tomorrow)
 Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
 1.[T][ ] first task
 2.[E][ ] third task (from: 2pm to: 3pm)
____________________________________________________________
____________________________________________________________
 OOPS!!! The task number must be a whole number.
____________________________________________________________
____________________________________________________________
 OOPS!!! Task 9 is not in the list.
____________________________________________________________
____________________________________________________________
 Bye. Hope to see you again soon!
____________________________________________________________
```
# Persistence checks

- Start with no `data/chris.txt`; add tasks and verify the directory and file are created.
- Restart Chris and verify saved todo, deadline, event, and completion status are restored.
- Mark, unmark, and delete tasks; restart and verify each change was saved.

# Date and time checks

- Add a deadline using `deadline return book /by 2026-12-02 1800`; verify it displays as `Dec 02 2026, 6:00PM`.
- Add an event with `/from` and `/to` values in `yyyy-MM-dd HHmm`; verify both are reformatted.
- Enter a malformed date and verify Chris explains the expected format without exiting.
